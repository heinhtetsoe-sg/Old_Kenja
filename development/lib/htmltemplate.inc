<?php

require_once('for_php7.php');

/**
* HTML template
* (C)Hiroshi Ayukawa.All rights reserved.
* License:BSD
* 2002.03.28   Ver. 1.2.1
* @access public
**/

class HtmlTemplate{
/**
* Interprit a file on memory and output the result.
* @access public
* @param String $file Filename
* @param Array $data a tree-like array
* @return void
*/
    function t_Include($file,$data){
        $val=$data;
        $all=fread(fopen($file,"rb"),filesize($file));
        $code=HtmlTemplate::_parsesrc($all);
        echo @eval('?>' .$code);
    }
/**
* Interprit a file on memory and require the result as a string.
* @access public
* @param String $file Filename
* @param Array $data a tree-like array
* @return void
*/
    function t_Buffer($file,$data){
        $val=$data;
        $all=fread(fopen($file,"rb"),filesize($file));
        $code=HtmlTemplate::_parsesrc($all);
        ob_start();
        echo @eval('?>' .$code);
        $ans=ob_get_contents();
        ob_end_clean();
        return $ans;
    }
    

    
    
/**
* Includes HTML file .
* @access public
* @param String $file filename
* @param Array $data tree-like array
* @param Array $dirname directoryname for .tmp file
* @return void
*/
    function t_Include_file($file,$data,$dirname="./"){
        HtmlTemplate::_htmltmp2($file,$data,$dirname);
    }


/**
* Require HTML file as a string.
* @access public
* @param String $file filename
* @param Array $data tree-like array
* @param Array $dirname directoryname for .tmp file
* @return String
*/
    function t_Buffer_file($file,$data,$dirname="./"){
        flush();
        ob_start();
        HtmlTemplate::_htmltmp2($file,$data,$dirname);
        $ans=ob_get_contents();
        ob_end_clean();
        return $ans;
    }

/**
* Compare the timestamp between .tmp & .html
* @access private
* @param String $file filename
* @param Array $data tree-like array
* @param Array $dirname directoryname for .tmp file
* @return void
*/
    function _htmltmp2($file,$data,$dirname){
        $val=$data;
        $t=$file.".tmp";
        if(substr($dirname,-1)!="/") $dirname.="/";
        if(!file_exists($dirname.dirname($t))) HtmlTemplate::_ForceDirectories($dirname.dirname($t));
        if(file_exists($dirname.$t)){
            if(filemtime($file)>filemtime($dirname.$t)) HtmlTemplate::_compile($file,$dirname);
        }else HtmlTemplate::_compile($file,$dirname);
            include($dirname.$t);
    }

/**
* Create directories for .tmp files
* @access private
* @param String $path path name
* @param Array $mode mode of the dir.
* @return void
*/
    function _ForceDirectories($path,$mode=0777) {
        if ( strlen($path) == 0) return 0;
        //
        if ( strlen($path) < 3) return 1; // avoid 'xyz:\' problem.
        elseif ( is_dir($path)) return 1; // avoid 'xyz:\' problem.
        elseif   ( dirname($path) == $path) return 1; // avoid 'xyz:\' problem.
        return ( HtmlTemplate::_ForceDirectories(dirname($path),$mode) and @mkdir( $path, $mode));
    }


/**
* Create .tmp file
* @access private
* @param String $tmpfile filename
* @return void
*/

    function _compile($tmpfile,$dirname="./"){
        $tm=preg_split("/\//",$tmpfile);
        if(file_exists($tmpfile)){

            $newfile=$dirname.$tmpfile.".tmp";
            $all=fread(fopen($tmpfile,"rb"),filesize($tmpfile));
            $fp=fopen($newfile,"w");
            fwrite($fp,HtmlTemplate::_parsesrc($all));
            ftruncate($fp,ftell($fp));
            fclose($fp);
        }
    }

/**
* Parse HTML strings.
* @access private
* @param String $str HTML strings.
* @return String
*/
    function _parsesrc($str){
        #translate \r\n to \n
        $str=str_replace("\r\n","\n",$str);

        $kuri=array();
        $str2=$str;
        $acc=1;

        # interpretation of <!-- {each }--><!--{/each}-->
        $kuri=array();
        preg_match_all("/<!--\{each ([^\}]+)\}-->/i",$str2,$k,PREG_SET_ORDER);
        while(list(,$x)=each($k)){
            $kuri[]=$x[1];
        }
        while(list(,$m)=each($kuri)){
            $ar=preg_split("/\//",$m);
            $ind="";
            $rui=array();
            $mattan=0;
            $loopid1=1;
            while(list(,$x)=each($ar)){
                array_push($rui,$x);
                if($mattan!=get_count($ar)-1 && in_array(join("/",$rui),$kuri)) {$ind.="[\"$x\"][\$cnt[\"".join("_",$rui)."\"]]";}
                else {$ind.="[\"$x\"]";}
                $mattan++;
            }
            $n=preg_replace("/\//","_",$m);
            $str2=str_replace("<!--{each $m}-->",
            "<?php
            for(\$cnt[\"$n\"]=0;\$cnt[\"$n\"]<get_count(\$val$ind);\$cnt[\"$n\"]++){
                ?>",
            $str2);
        }
        reset($kuri);

        $str2=str_replace("<!--{/each}-->",
        "<?php
        }
        ?>",$str2);


        # interpretation of {val }
        while(preg_match('/\{val ([^\}]+)\}/',$str2,$match)){
            $m=$match[1];
            $ar=preg_split("/\//",$m);
            $ind="";
            $rui=array();
            foreach($ar as $x){
                array_push($rui,$x);
                if(in_array(join("/",$rui),$kuri)){ $ind.="[\"".$x."\"][\$cnt[\"".join("_",$rui)."\"]]";}
                else {$ind.="[\"". $x."\"]";}
            }

            $str2=str_replace("{val $m}",
            "<?php print nl2br(\$val$ind); ?>",
            $str2);
        }


        # interpretation of {rval }
        while(preg_match('/\{rval ([^\}]+)\}/',$str2,$match)){
            $m=$match[1];
            $ar=preg_split("/\//",$m);
            $ind="";
            $rui=array();
            foreach($ar as $x){
                array_push($rui,$x);
                if(in_array(join("/",$rui),$kuri)){ $ind.="[\"".$x."\"][\$cnt[\"".join("_",$rui)."\"]]";}
                else {$ind.="[\"".$x."\"]";}
            }

            $str2=str_replace("{rval $m}",
            "<?php print \$val$ind; ?>",
            $str2);
        }

        # interpretation of <!-- {def }--><!--{/def}-->
        while(preg_match('/<!--\{def ([^\}]+)\}-->/i',$str2,$match)){
            $m=$match[1];
            $ar=preg_split("/\//",$m);
            $ind="";
            $rui=array();
            $mattan=0;
            foreach($ar as $x){
                array_push($rui,$x);
                if($mattan!=get_count($ar)-1 && in_array(join("/",$rui),$kuri)) {$ind.="[\"".$x."\"][\$cnt[\"".join("_",$rui)."\"]]";}
                else {$ind.="[\"".$x."\"]";}
                $mattan++;
            }
            $str2=str_replace("<!--{def $m}-->",
            "<?php
            if((gettype(\$val$ind)!='array' && \$val$ind!=\"\") or (gettype(\$val$ind)=='array' && get_count(\$val$ind)>0)){
                ?>",
            $str2);
        }
        $str2=str_replace("<!--{/def}-->",
        "<?php
        }
        ?>",
        $str2);


        return $str2;
    }

}

?>