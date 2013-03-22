

proc camel {s} {
    regsub -all {\&} $s {\&amp;} s
    regsub -all {\s\s+} $s { } s
    set c [string range $s 0 0]
    set r [string range $s 1 end]
    set s "[string toupper $c][string tolower $r]"
}


proc clean {s} {
    regsub -all {[^ -~]} $s  "" s
    set s
}

proc reset {} {

    set ::title ""
    set ::desc ""
    set ::date ""
    set ::url "" 
    set ::authors [list]
    set ::keywords [list]
    set ::inKeyword 0

    set ::type ""
    set ::doi ""
    set ::institution ""
    set ::city ""
    set ::publication ""
    set ::volume_number ""
    set ::issue_number ""
    set ::pages ""

}


proc textTag {tag text} {
    return  "<$tag><!\[CDATA\[$text\]\]></$tag>"
}


proc outputEntry {} {
    set author [lindex $::authors 0]
    
    set extra ""

    if {$::url!=""} {
        if {[regsub -all {\&} $::url {\&amp;} ::url] } {
#            puts stderr "GOT ONE: $::url"
        } else {
#            puts stderr "None: $::url"
        }
        append extra " url=\"$::url\" "
    }

    puts "<entry name=\"$::title\" type=\"biblio\" fromdate=\"$::date-01-01\" $extra >"
    puts [textTag primary_author $author]
    if {[llength $::authors]>1} {
        set otherAuthors [lrange $::authors 1 end]
        puts [textTag other_authors [join $otherAuthors "\n"]]
    }
    foreach var {type doi institution city publication volume_number issue_number pages} {
        set v [set ::$var]
        if {$v!=""} {
            puts [textTag $var $v]
        }
    }

    if {$::desc!=""} {
        puts "<description><!\[CDATA\[$::desc\]\]></description>"
    }
#    puts stderr "$::title"
    foreach key $::keywords {
        set key [camel $key]
        puts stderr "key: $key"
        puts "<metadata type=\"enum_tag\" attr1=\"$key\" />"

    }

    puts "</entry>"
}



puts "<entries>"


set c [read [open [lindex $argv 0] r]]

set inOne 0
reset

foreach line [split $c \n] {
    set line [string trim [clean $line]]
    if {$line == ""} continue;
    if {![regexp {(^%[^\s]+)\s+(.*$)} $line match tag value]} {
        if {$::inKeyword} {
            if {[regexp {doi} $line]} {
                set ::doi $line
                continue
            }
            lappend ::keywords $line
            continue
        }
        puts "Bad line: $line"
        exit
    }

    if {$tag == "%K"} {
        set ::inKeyword 1
        if {[regexp {doi} $line]} {
            set ::doi $line
            continue
        }
        lappend ::keywords $value
        continue
    }

    set ::inKeyword 0
    if {$tag == "%0"} {
        if {$inOne} {
            outputEntry
        }
        reset
        set ::type $value
        set inOne 1
        continue
    }
    if {$tag == "%A"} {
        lappend ::authors $value
        continue;
    }
    
    set gotit 0
    foreach pair {{%X desc} {%T title} {%R doi} {%C city} {%I institution} {%U url} {%J publication } {%V volume_number} {%N issue_number} {%P pages}  {%D date}} {
        foreach {flag var} $pair break
        if {$flag == $tag} {
            set gotit 1
            set ::$var $value
            break
        }
    }
    if {!$gotit} {
        continue
##        puts "NA: $tag"
    }



}

puts "</entries>"