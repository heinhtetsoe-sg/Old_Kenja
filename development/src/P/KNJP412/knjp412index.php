<?php

require_once('for_php7.php');

require_once('knjp412Model.inc');
require_once('knjp412Query.inc');

class knjp412Controller extends Controller {
    var $ModelClassName = "knjp412Model";

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
                case "knjp412":
                    $sessionInstance->knjp412Model();
                    $this->callView("knjp412Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp412Ctl = new knjp412Controller;
//var_dump($_REQUEST);
?>
