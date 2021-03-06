/*
 * Copyright (c) 2013-2017, Centre for Genomic Regulation (CRG).
 * Copyright (c) 2013-2017, Paolo Di Tommaso and the respective authors.
 *
 *   This file is part of 'Nextflow'.
 *
 *   Nextflow is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nextflow is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.nextflow.cwl

import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 *
 */
class StepTest extends Specification {

    def 'should extract the command string' () {

        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build

        inputs:
          indexfile:
            type: File
            inputBinding:
              position: 1
          doing:
            type: string
            inputBinding:
              position: 2
        '''
        .stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()
        Map stepinsTest = ['indexfile':'','doing':'']

        when:
        def cmd = step.extractCommandString(cwl, stepinsTest)
        then:
        cmd == 'bowtie2-build ${invar_0} ${invar_1}'

    }
    def 'check that extractCommandString handles defaults correctly' () {

        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build

        inputs:
          indexfile:
            type: File
            inputBinding:
              position: 1
          doing:
            type: string
            default: test
            inputBinding:
              position: 2
        '''
                .stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()
        Map stepinsTest = ['indexfile':'']

        when:
        def cmd = step.extractCommandString(cwl, stepinsTest)
        then:
        cmd == 'bowtie2-build ${invar_0} test'

    }
    def 'should extract arguments' (){
        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build
        arguments: ["-d", $(runtime.outdir)]
        
        inputs:
          indexfile:
            type: File
            inputBinding:
              position: 1
          doing:
            type: string
            inputBinding:
              position: 2
        '''.stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()

        when:
        def argsreturn = step.extractArguments(cwl)
        then:
        argsreturn == ' -d ./'
    }
    def 'check type returns' (){
        given:
        def step = new Step()

        when:
        def typeTest = step.cwlTypeConversion('File')
        then:
        typeTest == 'file'
        when:
        def typeTest2 = step.cwlTypeConversion('int')
        then:
        typeTest2 == 'val'
    }
    def 'secondary file formatting' (){
        given:
        def step = new Step()
        def secondaryFiles = ['../atestpath1','../atestpath2']

        when:
        def formatTest = step.formatSecondaryFiles(secondaryFiles)
        then:
        formatTest == ["file secondary_0 from file(../atestpath1)", "file secondary_1 from file(../atestpath2)"]
    }
    def 'check prefix parsing' (){
        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build
        
        inputs:
          gtffile:
            type: File
            inputBinding:
              position: 1
              prefix: --GTF
        '''.stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()

        when:
        def stepins = ['gtffile':'gtf']
        def cmdreturn = step.extractCommandString(cwl,stepins)
        then:
        cmdreturn == 'bowtie2-build --GTF ${invar_0}'


    }
    def 'check prefix parsing with = in' (){
        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build
        
        inputs:
          gtffile:
            type: File
            inputBinding:
              position: 1
              prefix: test=
        '''.stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()

        when:
        def stepins = ['gtffile':'gtf']
        def cmdreturn = step.extractCommandString(cwl,stepins)
        then:
        cmdreturn == 'bowtie2-build test=${invar_0}'
    }

}
