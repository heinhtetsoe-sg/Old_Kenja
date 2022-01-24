<?php

require_once('for_php7.php');

require_once('knjp373Model.inc');
require_once('knjp373Query.inc');

class knjp373Controller extends Controller {
    var $ModelClassName = "knjp373Model";
    var $ProgramID      = "knjp373";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp373":                         //メニュー画面もしくはSUBMITした場合
                case "change_class":                    //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp373Model();   //コントロールマスタの呼び出し
                    $this->callView("knjp373Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csv":         //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp373Form1");
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
$knjp373Ctl = new knjp373Controller;
//var_dump($_REQUEST);
?>
