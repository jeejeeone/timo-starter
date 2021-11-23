package fi.starter.backend

import zio.ZIO

package object aspect {
  implicit class EffectAspectSyntax[R, E, A](zio: ZIO[R, E, A]) {
    def @@(aspect: Aspect[R, E]): ZIO[R, E, A] = aspect.apply(zio)
  }
}

trait Aspect[-R, +E] {
  def apply[R1 <: R, E1 >: E, A](zio: ZIO[R1, E1, A]): ZIO[R1, E1, A]
}
