package utils

import tabby.Grid

object Nicknames:

  private val grid: Grid = {
    val resource = getClass.getClassLoader.getResource("names.csv")
    if (resource == null)
      throw new RuntimeException("names.csv not found in resources")
    Grid.fromURL(
      resource.toString,
      delim = ';'
    )
  }

  private val names: Set[String] =
    grid.mapRows(row => s"${row("förnamn")} ${row("efternamn")}").toSet

  def isValid(nickname: String): Boolean = names.contains(nickname)
