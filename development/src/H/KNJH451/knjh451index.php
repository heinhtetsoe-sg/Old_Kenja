<?php

require_once('for_php7.php');

require_once('knjh451Model.inc');
require_once('knjh451Query.inc');
require_once('graph.php');

class knjh451Controller extends Controller {
    var $ModelClassName = "knjh451Model";
    var $ProgramID      = "KNJH451";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "open":
                case "bottom_reappear":
                case "bottom_sanka":
                case "bottom_all":
                    $this->callView("knjh451Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":
                    if(!$sessionInstance->execCsv()){
                        $this->callView("knjh451Form1");
                    }
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = REQUESTROOT."/H/KNJH45MSEARCH/knjh45msearchindex.php?cmd=list&PROGRAMID=knjh451";
                    $args["edit_src"] = "knjh451index.php?cmd=edit";
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
$knjh451Ctl = new knjh451Controller;
//var_dump($_REQUEST);
?>
