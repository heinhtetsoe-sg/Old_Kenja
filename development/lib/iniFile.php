<?php

require_once('for_php7.php');

require_once('PEAR.php');

class IniFile extends PEAR {

    var $inifile;
    var $cached_section;
    var $cached_param;
    var $default_section = "";

    function &IniFile ( $filename = "", $section = "" )
    {
        if ( !empty($filename) ) {
            if ( $section != "") {
                $this->default_section = $section;
            }
            $ini = $this->setIniFile($filename);
        } else {
            $ini = PEAR::raiseError("IniFile Model: INI File Required");
        }
        return $ini;
    }

    function &setIniFile($filename)
    {
        if ( !file_exists($filename) ) {
            return PEAR::raiseError("IniFile Model: INI File Required");
        }
        
        if($this->default_section != "") $sec = strtolower($this->default_section);
        $arrlines = file($filename);
        foreach ($arrlines as $line_num => $line_buf) {
            $line = trim($line_buf);
            if ( mb_ereg("^[ 　]*(;|\#)",$line) ) {
                continue;
            } 

            if (mb_ereg("#",$line) ) {
                $line = preg_replace("/#.*$/","",$line);
            }

            if ( empty($line) ) {
                continue;
            } elseif ( preg_match("/^\[(.*)\]/",$line, $ar) ) {
                $this->cached_section[] = $ar[1];
                if ($this->default_section == "") $sec = strtolower($ar[1]);
            } elseif ( preg_match("/(\w*)\s*=\s*(.*?)\Z/", $line, $ar) ) {
                $variable = strtolower(trim($ar[1]));
                $value    = trim($ar[2]);
                $this->cached_param[$sec][$variable] = $value;
            }
        }

        $this->inifile = $filename;
    }

    function sections() 
    {
        reset($this->cached_section);
        $ar = array();
        foreach ( $this->cached_section as $key => $value ) {
            $ar[] = $value;
        } 
        return $ar;
    }

    function section($section = "") 
    {
        if ($section == "" ) {
            if ($this->default_section == "" ) {
                die("parameter is bad : section_name");
            }     
            $sec = $this->default_section;
        } else {
            $sec = strtolower($section);
        }
    //    reset($this->cached_param);
        $ar = array();
        foreach ( $this->cached_param[$sec] as $key => $value ) {
            $ar[$key] = $value;
        }
        return $ar;
    }

    function parameter($section , $variable, $default = "") 
    {
        $sec = strtolower($section);
        $var = strtolower($variable);
        // reset($this->cached_param);
        $ar = array();
        if ( isset($this->cached_param[$sec][$var]) ) {
            return $this->cached_param[$sec][$var];
        } else {
            return $default;
        }
    }
    
    function param($section, $variable="", $default = "") 
    {
        if ( $this->default_section != "") {
            return $this->parameter($this->default_section, $section, $variable);
        } else {
           // if ( $variable == "") die("parameter is bad : variable");
            return $this->parameter($section,$variable,$default);
        }
    }
    
}
function i18n_convert() {
   $numargs = func_num_args();
   $arg_list = func_get_args();
   $buff=$arg_list[0];
   if ($numargs==2) {
       return mb_convert_encoding($buff,$arg_list[1]);
   }
   else if ($numargs==3) {
       return mb_convert_encoding($buff,$arg_list[1],$arg_list[2]);
   }
}
function i18n_ja_jp_hantozen() {
   $numargs = func_num_args();
   $arg_list = func_get_args();
   $buff=$arg_list[0];
   if ($numargs==2) {
       return mb_convert_kana($buff,$arg_list[1]);
   }
   else if ($numargs==3) {
       return mb_convert_kana($buff,$arg_list[1],$arg_list[2]);
   }
}
function i18n_http_output() {
   $numargs = func_num_args();
   if ($numargs==1) {
       $str=func_get_arg(0);
       return mb_http_output($str);
   }
   else {
       return mb_http_output();
   }
}
?>
