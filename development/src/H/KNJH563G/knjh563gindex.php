<?php

require_once('for_php7.php');

require_once('knjh563gModel.inc');
require_once('knjh563gQuery.inc');

class knjh563gController extends Controller {
    var $ModelClassName = "knjh563gModel";
    var $ProgramID      = "KNJH563G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh563g":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh563gModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh563gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh563gForm1");
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
$knjh563gCtl = new knjh563gController;
//var_dump($_REQUEST);
?>
