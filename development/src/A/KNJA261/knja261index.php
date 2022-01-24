<?php

require_once('for_php7.php');

require_once('knja261Model.inc');
require_once('knja261Query.inc');

class knja261Controller extends Controller {
    var $ModelClassName = "knja261Model";
    var $ProgramID      = "KNJA261";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv": // CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja261Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "knja261": // メニュー画面もしくはSUBMITした場合
                case "change_class": // クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja261Model();
                    $this->callView("knja261Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja261Ctl = new knja261Controller;
//var_dump($_REQUEST);
?>
