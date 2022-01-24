<?php

require_once('for_php7.php');

require_once('knje380Model.inc');
require_once('knje380Query.inc');

class knje380Controller extends Controller {
    var $ModelClassName = "knje380Model";
    var $ProgramID      = "KNJE380";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje380":                         //メニュー画面もしくはSUBMITした場合
                case "change_grade":                    //クラス変更時のSUBMITした場合
                    $sessionInstance->knje380Model();   //コントロールマスタの呼び出し
                    $this->callView("knje380Form1");
                    exit;
                case "csv1":
                case "csv2":
                case "csv3":
                case "csv4":
                case "csv5":
                case "csv6":
                case "csv7":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje380Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje380Ctl = new knje380Controller;
#var_dump($_REQUEST);
?>
