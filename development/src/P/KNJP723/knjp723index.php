<?php

require_once('for_php7.php');

require_once('knjp723Model.inc');
require_once('knjp723Query.inc');

class knjp723Controller extends Controller {
    var $ModelClassName = "knjp723Model";
    var $ProgramID      = "KNJP723";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjp723Form1");
                    break 2;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp723Form1");
                    }
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
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
$knjp723Ctl = new knjp723Controller;
//var_dump($_REQUEST);
?>
