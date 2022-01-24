<?php

require_once('for_php7.php');

require_once('knjz670Model.inc');
require_once('knjz670Query.inc');
require_once('graph.php');

class knjz670Controller extends Controller {
    var $ModelClassName = "knjz670Model";
    var $ProgramID      = "KNJZ670";
    
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
                    $this->callView("knjz670Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = REQUESTROOT."/U/KNJZPSEARCH/knjzpsearchindex.php?cmd=list&PROGRAMID=knjz670";
                    $args["edit_src"] = "knjz670index.php?cmd=edit";
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
$knjz670Ctl = new knjz670Controller;
//var_dump($_REQUEST);
?>
