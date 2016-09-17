# DimerRemover

Project previously hosted here: https://sourceforge.net/projects/dimerremover/

## CONTENT

1. DESCRIPTION
2. REQUIREMENTS
3. OPTIONS
4. USAGE
5. DETAILS
6. CHANGELOG

## 1. DESCRIPTION

When doing primary analysis on NGS data, one sometimes faces huge amount
of adapter dimers among the sequences. These sequences are completely
useless, so it is a good idea to get rid of them. Some programs already
do that, mostly using alignment, and they basically allow to trim the
reads from 3' adapter sequences as well. But none of them is dedicated
only to adapter dimers, and they can be very slow. If you want to take a
look at some of these programs, here are some links:
  - Trim Galore!: http://www.bioinformatics.babraham.ac.uk/projects/trim_galore/
  - Trimmomatic: http://www.usadellab.org/cms/?page=trimmomatic
  - cutadapt: https://code.google.com/p/cutadapt/

DimerRemover was designed to specifically remove dimers in the fastest
way possible. It keeps paired end files synchronized. The dimer reads
are not automatically deleted, they are written in another file. You can
just count the dimers without writing output (it is faster).


## 2. REQUIREMENTS

Java 7 JVM


## 3. OPTIONS

By default, input and output files are gzip compressed fastq files
(fastq.gz).
```
  -i, --input <path>            first input file
  -j, --inputR2 <path>          second input file for paired end data
  --output-nodimer <path>       output file that will contain reads from
                                the first input file that are not 
                                adapter dimers
  --output-nodimerR2 <path>     same for the second input file
  --output-dimer <path>         output file that will contain the dimers
                                from the first input file
  --output-dimerR2 <path>       same for the second input file
  -a, --adapter <string>        nucleotide sequence used to detect
                                adapter dimers
  --log <path>                  an output file containing the results
                                and some performance statistics     
  -c, --count                   flag indicating that no output file will
                                be written, the program will only count
                                the dimers
  -s, --SID <string>            a string representing the variations
                                undergone by the adapter sequence. See
                                details section. Default="i2:s2(d1):d2"
  --hashfile <path>             output binary file storing the hash
                                created from the adapter sequence
  --restorehash <path>          use a previously saved hash instead of
                                an adapter sequence
  --readshift <int>             parameter with minor effect. See details
                                section. Default=5
  --adaptershift <int>          parameter with minor effect. See details
                                section. Default=5
  --compression-level <int>     set the gzip compression level of output
                                files, must be between 0 and 9.
                                Default=4
  --uncompressed-input          flag indicating that the input is in
                                fastq format
  --uncompressed-output         flag indicating that the output will be
                                in fastq format

  -h, --help                    display the program's help
  -v, --version                 display the program's version
  --debug                       verbose output
```

## 4. USAGE

Keep adapter dimers in separate file for single read data:
```bash
  java -jar dimerremover.jar -i R1.fastq.gz --output-nodimer nodimer.fastq.gz \ 
        --output-dimer dimer.fastq.gz -a AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
```

Keep only non-dimers reads in paired end data and save the hash:
```bash
  java -jar dimerremover.jar -i R1.fastq.gz -j R2.fastq.gz \
        --output-nodimer nodimer.fastq.gz --output-nodimerR2 nodimerR2.fastq.gz \
        --output-dimer /dev/null --output-dimerR2 /dev/null \
        -a AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC \
        --hashfile hash.bin
```

Use previously saved hash on other files:
```bash
  java -jar dimerremover.jar -i R1.fastq.gz --output-nodimer nodimer.fastq.gz \
        --output-dimer /dev/null --restorehash hash.bin
```

Integrate the program in a pipe, with a previously uncompressed fastq:
```bash
  mkfifo R1.fastq.fifo
  zcat R1.fastq.gz | do_some_stuff_on_fastq > R1.fastq.fifo &
  java -jar dimerremover.jar -i R1.fastq.fifo --output-nodimer nodimer.fastq.gz \ 
        --output-dimer dimer.fastq.gz -a AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC \
        --uncompressed-input
```


## 5. DETAILS

 * Algorithm details

This program uses a hash table to store variations of the adapter
sequence as keys. It then takes substrings of the beginning of reads and
check if they can be found in this hash. In that case the read is
considered to be a dimer.

 * Performance considerations

This program is I/O bound, which means it is basically limited by the
speed at which you can write and read from the disk. Using a hash to
match strings is faster enough to make computation negligible.
Compressing and writing the output take most of the time, that is why
you will notice a significant increase of performance with the -c
option, or when lowering the compression level.

This section will be further filled.


## 6. CHANGELOG

 * v0.9.2
  - fixed a thread race condition happening at the end of execution
  - added a debug mode (more messages to come in the future)
  - displaying version does not require to provide -i and -l options
    anymore

 * v0.9.1
  - removed unnecessary debug message "Fill buffer i"
  - added a --version option
  - now works with read identifiers that do not start with @HWI
  - added the percentage of input reads forming dimers in the output
  - decimals now written in english format in output

 * v0.9
  - added option to define the output compression level
  - added a log file
  - possibility to get and produce uncompressed fastq
  - providing output files is no longer necessary when only counting the
    dimers
