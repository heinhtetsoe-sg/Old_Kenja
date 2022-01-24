<?php

require_once('for_php7.php');

require_once('knjh569Model.inc');
require_once('knjh569Query.inc');

class knjh569Controller extends Controller {
    var $ModelClassName = "knjh569Model";
    var $ProgramID      = "KNJH569";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh569":                             //メニュー画面もしくはSUBMITした場合
                case "chgGrade":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh569Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh569Form1");
                    exit;

                case "csv":     //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh569Form1");
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
$knjh569Ctl = new knjh569Controller;
?>
