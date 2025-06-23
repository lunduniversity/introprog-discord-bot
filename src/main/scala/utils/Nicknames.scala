package utils

object Nicknames:

  private val names: Set[String] =
    Set("Björn Regnell", "Ebbe Flisbäck", "Lovisa Löfgren", "William Sonesson")

  def isValid(nickname: String): Boolean = names.contains(nickname)
