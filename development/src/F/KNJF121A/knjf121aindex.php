<?php

require_once('for_php7.php');

require_once('knjf121aModel.inc');
require_once('knjf121aQuery.inc');

class knjf121aController extends Controller {
    var $ModelClassName = "knjf121aModel";
    var $ProgramID      = "KNJF121A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf121a":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf121aModel();  //コントロールマスタの呼び出し
                    $this->callView("knjf121aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf121aForm1");
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
$knjf121aCtl = new knjf121aController;
//var_dump($_REQUEST);
?>
