<?php

require_once('for_php7.php');

require_once('knjd328Model.inc');
require_once('knjd328Query.inc');

class knjd328Controller extends Controller {
    var $ModelClassName = "knjd328Model";
    var $ProgramID      = "KNJD328";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd328":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd328Model();   //コントロールマスタの呼び出し
                    $this->callView("knjd328Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd328Form1");
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
$knjd328Ctl = new knjd328Controller;
//var_dump($_REQUEST);
?>
