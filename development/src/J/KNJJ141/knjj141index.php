<?php

require_once('for_php7.php');

require_once('knjj141Model.inc');
require_once('knjj141Query.inc');

class knjj141Controller extends Controller {
    var $ModelClassName = "knjj141Model";
    var $ProgramID      = "KNJJ141";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knjj141Form1");
                    break 2;
                case "knjj141":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjj141Model();        //コントロールマスタの呼び出し
                    $this->callView("knjj141Form1");
                    exit;
                case "csv":         //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj141Form1");
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
$knjj141Ctl = new knjj141Controller;
?>
