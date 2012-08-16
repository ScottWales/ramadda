
##
## Call this with:
## tclsh zipandclean.tct <directories to walk>
##

##
##This script recurses down a given set of directories and replaces spaces with "_" in file and directory names
##Any .RiSCAN directories are zipped up and the original directory is removed
##


proc checkFile {f} {

# Clean up the name

#change spaces for "_"
    regsub -all { +} $f _ newfile

#this would convert dashes to "_" but for now lets not do that
#   regsub -all {-} $newFile _ newfile

#If the name changed then move the file
    if {$f != $newFile} {
        puts "renaming $f to $newfile"
        file rename -force $f $newfile
        set f $newfile
    }

    if  {[file isdirectory $f]} {
#If its a RiSCAN directory then zip it up and return
        if  {[regexp {\.RiSCAN$} $f]} {
            set dir [file dirname $f]
            set name [file tail $f]
            set zipName $name.zip
            puts "Zipping  RiSCAN: $f"
            exec jar  -cvfM $dir/$zipName  -C $dir $name
            return
        }

#Recurse down the tree
        foreach child [glob -nocomplain $f/*] {
            checkFile $child
        }
    }

}


foreach f $argv {
    checkFile $f
}