<?php

require_once('for_php7.php');

require_once('knjmp959Model.inc');
require_once('knjmp959Query.inc');

class knjmp959Controller extends Controller {
    var $ModelClassName = "knjmp959Model";

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
                case "knjmp959":
                    $sessionInstance->knjmp959Model();
                    $this->callView("knjmp959Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp959Ctl = new knjmp959Controller;
//var_dump($_REQUEST);
?>
