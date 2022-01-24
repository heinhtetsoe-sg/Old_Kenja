<?php

require_once('for_php7.php');

require_once('knjb3023Model.inc');
require_once('knjb3023Query.inc');

class knjb3023Controller extends Controller {
    var $ModelClassName = "knjb3023Model";
    var $ProgramID      = "KNJB3023";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "editStaff":
                case "reset":
                case "getHrClass":
                case "getClass":
                case "getSubclass":
                case "getCreditHrClass":
                case "getLayoutClassStaff":
                    $this->callView("knjb3023Form1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $sessionInstance->setCmd("edit");
                        break 1;
                    }
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
$knjb3023Ctl = new knjb3023Controller;
//var_dump($_REQUEST);
?>
