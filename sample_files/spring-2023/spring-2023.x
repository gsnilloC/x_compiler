program {
  // TYPE -> 'string'
  // TYPE -> 'hex
  // F -> <string>
  // F -> <hex>
  int result string s string t hex h hex i
  s = @test string@
  h = 0xabcdef

  // E -> SE '>' SE
  result = s > t
  // E -> SE '>=' SE
  result = h >= i
  // T -> T '%' F
  result = 7 % 2 == 1

  // S -> 'if' E 'then' BLOCK (without else)
  if(result) then {
    result = write(1)
  }

  // S -> 'unless' E 'then' BLOCK
  unless(result) then {
    result = write(42)
  }

  // S -> 'select' NAME SELECT_BLOCK
  // SELECT_BLOCK -> '{' SELECTOR+ '}'
  // SELECTOR -> '[' E ']' '->' BLOCK
  select i {
    [1 == 1] -> { result = write(7) }
    [1 == 0] -> { result = write(9) }
  }
}