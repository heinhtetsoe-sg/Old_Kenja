<?php

require_once('for_php7.php');

require_once('knjp371Model.inc');
require_once('knjp371Query.inc');

class knjp371Controller extends Controller {
    var $ModelClassName = "knjp371Model";
    var $ProgramID      = "knjp371";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp371":                         //メニュー画面もしくはSUBMITした場合
                case "change_class":                    //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjp371Model();   //コントロールマスタの呼び出し
                    $this->callView("knjp371Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csv":         //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp371Form1");
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
$knjp371Ctl = new knjp371Controller;
//var_dump($_REQUEST);
?>
