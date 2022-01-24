<?php
require_once('knjh815Model.inc');
require_once('knjh815Query.inc');
require_once('graph.php');

class knjh815Controller extends Controller {
    var $ModelClassName = "knjh815Model";
    var $ProgramID      = "KNJH815";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/V/KNJVEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/H/KNJh815/knjh815index.php?cmd=edit")."&button=1";
                    $args["right_src"] = "knjh815index.php?cmd=edit";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "kubun_change":
                case "kaisu_change":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh815Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh815Form1");
                    }
                    break 2;
                    
                
                case "graph":
                    $this->callView("knjh815Form2");
                    break 2;

                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJh815Ctl = new knjh815Controller;
//var_dump($_REQUEST);
?>
