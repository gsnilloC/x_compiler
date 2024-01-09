program {
  int j string s hex h boolean b

  {
    s = @hello world!@
    h = 0xa1b2c3
    j = 42
  }

  {
    if(1 > 2) then {
      j = 1
    }

    if(2 >= 3) then {
      b = 1 + 2
    } else {
      j = j + j - j / j * j
    }

    unless(3 > 4) then {
      j = j + 2
    }
  }

  select i {
    [ 1 == 1 ] -> { j = j % j }
    [ 1 == 0 ] -> { j = j + 42 }
  }
}
