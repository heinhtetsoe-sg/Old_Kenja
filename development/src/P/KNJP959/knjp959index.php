<?php

require_once('for_php7.php');

require_once('knjp959Model.inc');
require_once('knjp959Query.inc');

class knjp959Controller extends Controller {
    var $ModelClassName = "knjp959Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //CSV取込
                    if (!$sessionInstance->getDownloadModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("main");
                        break 1;
                    }
                    break 2;
                case "":
                case "main":
                case "knjp959":
                    $sessionInstance->knjp959Model();
                    $this->callView("knjp959Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp959Ctl = new knjp959Controller;
//var_dump($_REQUEST);
?>
