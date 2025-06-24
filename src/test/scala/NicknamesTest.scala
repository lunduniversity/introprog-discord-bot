package utils

class NicknamesTest extends munit.FunSuite {

  test("isValid should return true for valid names from CSV") {
    assert(Nicknames.isValid("Björn Regnell"))
    assert(Nicknames.isValid("Ebbe Flisbäck"))
    assert(Nicknames.isValid("Lovisa Löfgren"))
    assert(Nicknames.isValid("William Sonesson"))
  }

  test("isValid should return false for invalid names") {
    assert(!Nicknames.isValid("John Doe"))
    assert(!Nicknames.isValid("Invalid Name"))
    assert(!Nicknames.isValid("Random Person"))
  }

  test("isValid should return false for empty string") {
    assert(!Nicknames.isValid(""))
  }

  test("isValid should return false for partial names") {
    assert(!Nicknames.isValid("Björn"))
    assert(!Nicknames.isValid("Regnell"))
    assert(!Nicknames.isValid("Ebbe"))
  }

  test("isValid should return false for names with extra spaces") {
    assert(!Nicknames.isValid(" Björn Regnell"))
    assert(!Nicknames.isValid("Björn Regnell "))
    assert(!Nicknames.isValid("Björn  Regnell"))
  }

  test("isValid should return false for names with different casing") {
    assert(!Nicknames.isValid("björn regnell"))
    assert(!Nicknames.isValid("BJÖRN REGNELL"))
    assert(!Nicknames.isValid("Björn regnell"))
  }

  test("isValid should handle Swedish characters correctly") {
    assert(Nicknames.isValid("Björn Regnell"))
    assert(Nicknames.isValid("Ebbe Flisbäck"))
    assert(Nicknames.isValid("Lovisa Löfgren"))

    assert(!Nicknames.isValid("Bjorn Regnell"))
    assert(!Nicknames.isValid("Ebbe Flisback"))
  }
}
