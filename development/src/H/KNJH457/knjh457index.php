<?php

require_once('for_php7.php');

require_once('knjh457Model.inc');
require_once('knjh457Query.inc');
require_once('graph.php');

class knjh457Controller extends Controller {
    var $ModelClassName = "knjh457Model";
    var $ProgramID      = "KNJH457";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "open":
                case "syubetu_change":
                case "bottom_reappear":
                case "bottom_sanka":
                case "bottom_all":
                    $this->callView("knjh457Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->execCsv()){
                        $this->callView("knjh457Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = REQUESTROOT."/H/KNJH45PSEARCH/knjh45psearchindex.php?cmd=list&PROGRAMID=knjh457";
                    $args["edit_src"] = "knjh457index.php?cmd=edit";
                    $args["rows"] = "113px,*";
                    View::frame($args,  "frame3.html");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh457Ctl = new knjh457Controller;
//var_dump($_REQUEST);
?>
