package cats
package mtl

import cats.data._
import cats.syntax.all._

trait MonadFunctor[T[_[_], _]] extends FunctorFunctor[T] {
  def instanceM[F[_] : Monad]: Monad[CurryT[T, F]#l]
  def mapTS[F[_]: Functor, G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]
  def mapTT[F[_], G[_]: Functor, A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}

object MonadFunctor {
  implicit def eithertTFunctor[E]: MonadFunctor[EitherTCE[E]#l] = {
    new MonadFunctor[EitherTCE[E]#l] {
      def mapTS[F[_]: Functor, G[_], A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))
      def mapTT[F[_], G[_]: Functor, A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))

      def instanceM[F[_] : Monad]: Monad[EitherTC[F, E]#l] = EitherT.catsDataMonadErrorForEitherT
    }
  }

  implicit def readertTFunctor[E]: MonadFunctor[ReaderTCE[E]#l] = {
    new MonadFunctor[ReaderTCE[E]#l] {
      def mapTS[F[_]: Functor, G[_], A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)
      def mapTT[F[_], G[_]: Functor, A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)

      def instanceM[F[_] : Monad]: Monad[ReaderTC[F, E]#l] = ReaderT.catsDataMonadReaderForKleisli
    }
  }

  implicit def writertTFunctor[L: Monoid]: MonadFunctor[WriterTCL[L]#l] = {
    new MonadFunctor[WriterTCL[L]#l] {
      def mapTS[F[_]: Functor, G[_], A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))
      def mapTT[F[_], G[_]: Functor, A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))

      def instanceM[F[_] : Monad]: Monad[WriterTC[F, L]#l] = WriterT.catsDataMonadWriterForWriterT[F, L]
    }
  }

  implicit def statetTFunctor[S]: MonadFunctor[StateTCS[S]#l] = {
    new MonadFunctor[StateTCS[S]#l] {
      def mapTS[F[_]: Functor, G[_], A](tfa: StateT[F, S, A])(trans: F ~> G): StateT[G, S, A] = StateT.applyF[G, S, A](trans(tfa.runF.map(a => a.andThen(trans(_)))))
      def mapTT[F[_], G[_]: Functor, A](tfa: StateT[F, S, A])(trans: F ~> G): StateT[G, S, A] = StateT.applyF[G, S, A](trans(tfa.runF).map(a => a.andThen(trans(_))))

      def instanceM[F[_] : Monad]: Monad[StateTC[F, S]#l] = StateT.catsDataMonadStateForStateT[F, S]
    }
  }

  implicit def optiontTFunctor[E]: MonadFunctor[OptionT] = {
    new MonadFunctor[OptionT] {
      def mapTS[F[_]: Functor, G[_], A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))
      def mapTT[F[_], G[_]: Functor, A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))

      def instanceM[F[_] : Monad]: Monad[OptionTC[F]#l] = OptionT.catsDataMonadForOptionT
    }
  }
}