package scalabot

package irc

import scalabot.pretty.Pretty

import akka.actor.{ Actor, ActorRef, Props, ActorLogging, Deploy }
import akka.util.ByteString
import akka.io.TcpReadWriteAdapter
import akka.io.{
  TcpPipelineHandler,
  BackpressureBuffer,
  DelimiterFraming,
  IO,
  SslTlsSupport,
  StringByteStringAdapter,
  Tcp
}
import akka.event.Logging

import java.net.InetSocketAddress
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.{
  KeyManagerFactory,
  TrustManager,
  TrustManagerFactory,
  SSLContext
}
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext


object Connection {
  def props(remote: InetSocketAddress, ssl : Boolean, replies: ActorRef) =
    Props(classOf[Connection], remote, ssl, replies)
}

class Connection(remote: InetSocketAddress, ssl : Boolean, listener: ActorRef) extends Actor {

  import Tcp._
  implicit val system = context.system

  val manager = IO(Tcp)
  val counter = new AtomicInteger

  manager ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) => {
      val host = remote.toString
      Pretty.red(s"Connection to $host failed.")
      listener ! "failed"
      context stop self
    }
    case c @ Connected(remote, local) => {
      val host = remote.toString
      Pretty.green(s"Connected to $host.")
      listener ! c
      val connection = sender
      if (ssl) {
        val sslContext = getSSLContext()
        val sslEngine = sslContext.createSSLEngine(
          remote.getHostName(), remote.getPort())
        sslEngine.setUseClientMode(true)
        Pretty.magenta(s"SSL? $ssl $sslEngine")
        val init = TcpPipelineHandler.withLogger(system.log,
          new StringByteStringAdapter >>
            new DelimiterFraming(
              maxSize = 1024,
              delimiter = ByteString('\n'),
              includeDelimiter = true
            ) >>
            new TcpReadWriteAdapter >>
            new SslTlsSupport(sslEngine
        ))
        val pipeline = system.actorOf(
          TcpPipelineHandler.props(init, connection, self).withDeploy(Deploy.local),
          "client" + counter.incrementAndGet())
        connection ! Tcp.Register(pipeline)
      } else {
        connection ! Register(self)
      }
      context become {
        case data: ByteString => {
          connection ! Write(data)
        }
        case CommandFailed(w: Write) => Pretty.red(s"Write to $host failed")
        case Received(data) => {
          listener ! data
        }
        case "close" => {
          Pretty.yellow(s"Requested conection to $host be closed.")
          connection ! Close
        }
        case _: ConnectionClosed => {
          Pretty.yellow(s"Closed conection to $host.")
          context stop self
        }
      }
    }
  }
  def getSSLContext() = {

    val passphrase = "lolwtf".toCharArray
    val ksKeys = KeyStore.getInstance("JKS");
    ksKeys.load(null)
    val ksTrust = KeyStore.getInstance("JKS");
    ksTrust.load(null)

    // KeyManager's decide which key material to use.
    val kmf =
        KeyManagerFactory.getInstance("SunX509");
    kmf.init(ksKeys, passphrase);

    // TrustManager's decide whether to allow connections.
    val tmf =
        TrustManagerFactory.getInstance("SunX509");
    tmf.init(ksTrust);

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(
        kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    sslContext
  }
}

