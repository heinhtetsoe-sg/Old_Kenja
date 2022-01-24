<?php

require_once('for_php7.php');

require_once('knjp731Model.inc');
require_once('knjp731Query.inc');

class knjp731Controller extends Controller {
    var $ModelClassName = "knjp731Model";
    var $ProgramID      = "KNJP731";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update": // CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp731Form1");
                    }
                    break 1;
                case "csv": // CSV出力
                    if (!$sessionInstance->outPutCsv()){
                        $this->callView("knjp731Form1");
                    }
                    break 2;
                case "":
                case "updateEnd":
                case "knjp731": // メニュー画面もしくはSUBMITした場合
                case "change_class": // クラス変更時のSUBMITした場合
                    $sessionInstance->knjp731Model();
                    $this->callView("knjp731Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp731Ctl = new knjp731Controller;
//var_dump($_REQUEST);
?>
