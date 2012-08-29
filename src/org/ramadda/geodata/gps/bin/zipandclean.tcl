
##
## Call this with:
## tclsh zipandclean.tct <directories to walk>
##

##
##This script recurses down a given set of directories and replaces spaces with "_" in file and directory names
##Any .RiSCAN directories are zipped up and the original directory is removed
##


set ::test 0



proc getFlag {flag dflt}  {
    if {[info exists ::flags($flag)]} {
        return $::flags($flag)
    }
    return $dflt
}

proc checkFile {f patterns zipPatterns skip} {

# Clean up the name
    set newFile $f

    foreach tuple $patterns {
        foreach {from to} $tuple break
        regsub -all $from $newFile $to newFile
    }


#change spaces for "_"
    regsub -all { +} $newFile _ newFile

    set exp {(.*|^)(20(08|09|10|11|12))-(01|02|03|04|05|06|07|08|09|10|11|12)-([012]\d|30|31)(.*)}
    regsub  $exp $newFile {\1\2_\4_\5\6} newFile


#Check for yyyymmdd and convert it to yyyy_mm_dd
    set exp {(.*|^)(20(08|09|10|11|12))(01|02|03|04|05|06|07|08|09|10|11|12)([012]\d|30|31)(.*)}
    regsub  $exp $newFile {\1\2_\4_\5\6} newFile


#If the name changed then move the file
    if {$f != $newFile} {
        if {$::test} {
            puts "test: renaming $f to $newFile"
        } else {
            puts "renaming $f to $newFile"
            file rename -force $f $newFile
            set f $newFile
        }
    }

    if  {[file isdirectory $f]} {
        foreach pattern $skip {
            if  {[regexp $pattern $f]} {
                puts "skipping: $f"
                return
            }
        }

        #If its one of the zip patterns then zip it up and return
        set zipit 0
        foreach pattern $zipPatterns {
            if  {[regexp $pattern $f]} {
                set zipit 1
                break
            }
        }
        if {$zipit} {
            set dir [file dirname $f]
            set name [file tail $f]
            set zipName $name.zip
            if {$::test} {
                puts "test: Zipping  directory: $f"
                puts "test: Deleting original directory"
            } else {
                puts "Zipping  directory: $f"
                exec jar  -cvfM $dir/$zipName  -C $dir $name
                puts "Deleting original directory"
                file delete -force $f
            }
            return
        }

#Recurse down the tree
        foreach child [glob -nocomplain $f/*] {
            checkFile $child $patterns $zipPatterns $skip
        }
    }
}

set patterns [list]
set zipPatterns  [list]
set skip  [list]


for {set i 0} {$i<$argc} {incr i} {
    set arg [lindex $argv $i]
    if {$arg =="-help"} {
        puts "usage: zipandclean.tcl <-test> <-convert from to> <-zip pattern> <-skip pattern> \[directories to recurse\]"
        exit
    }

    if {$arg =="-convert"} {
        incr i
        set from [lindex $argv $i]
        incr i
        set to [lindex $argv $i]
        lappend patterns [list $from $to]
   } elseif {$arg =="-test"} {
       set ::test 1
    } elseif {$arg =="-zip"} {
        incr i
        lappend zipPatterns [lindex $argv $i]
    } elseif {$arg =="-skip"} {
        incr i
        lappend skip [lindex $argv $i]
    } else {
        checkFile $arg $patterns $zipPatterns $skip
    }
}