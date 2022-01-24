<?php

require_once('for_php7.php');

require_once('knjh563qModel.inc');
require_once('knjh563qQuery.inc');

class knjh563qController extends Controller {
    var $ModelClassName = "knjh563qModel";
    var $ProgramID      = "KNJH563Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh563q":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh563qModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh563qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh563qForm1");
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
$knjh563qCtl = new knjh563qController;
//var_dump($_REQUEST);
?>
