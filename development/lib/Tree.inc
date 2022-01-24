<?php

require_once('for_php7.php');

require_once("PHPlib/tree.inc");

class TreeView extends Tree {
    var $tree_info = array();
    function starttree () {
//		$this->outp.="<ul id=\"foldinglist\" style=\"display:none\">\n";
        $this->flag=true;
    }
    function growtree ($key,$value,$path,$depth,$count,$pcount) {
        if ($this->flag) {
            $this->outp ="<ul id=\"foldinglist\" >\n";
        } elseif ($count==$pcount) {
        } else {
        }
        $this->outp.= "<li id=\"foldheader\">$value</li>\n";
        $this->prfx[$depth]="";
        $this->flag=true;
    }

    function leaftree ($key,$value,$path,$depth,$count,$pcount) {
        if ($this->flag) {
            if ($depth == 1){
                $this->outp.="<ul id=\"foldinglist\" >\n";
            }else{
                $this->outp.="<ul id=\"foldinglist\" style=\"display:none\">\n";
            }
        } elseif ($count==$pcount) {
        } else {
        }
        $this->outp.= "<li>$value</li>\n";
        $this->flag=false;
    }

    function shrinktree ($key,$depth) {
        $this->outp.= "</ul>\n";
    }
    //ツリー作成
    function build_tree ($code = 0)
    {
        $this->tree = $this->recurs_tree($code,0);
    }
    function addTree($code, $parent, $val)
    {
        $this->tree_info[$parent][$code] = $val;
    }

    function set_val ($val)
    {
        return $val;
    }
    function recurs_tree ($code, $depth)
    {
        if ( is_array($this->tree_info[$code]) ) {
            foreach($this->tree_info[$code] as $p => $v){
                if (!isset($array[0])){
                    $array[0] = "";
                }
                if (is_array($this->tree_info[$p])) {
                    $array[$p] = $this->recurs_tree($p, (int)$depth+1);
                    #注意: 配列[0]を埋めるのは、配列の残りの部分の後
                    #にすることが重要です!
                    $array[$p][0] = $this->set_val ($v);
                } else {
                    $array[$p] = $this->set_val ($v);
                }
            }
        }
        return($array);
    }
    function endtree () {
        $this->outp.= "</ul>";
    }

}
?>
