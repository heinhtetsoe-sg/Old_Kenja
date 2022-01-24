<?php

require_once('for_php7.php');

require_once('knjd192dModel.inc');
require_once('knjd192dQuery.inc');

class knjd192dController extends Controller {
    var $ModelClassName = "knjd192dModel";
    var $ProgramID      = "KNJD192D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd192d":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd192dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192dForm1");
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
$knjd192dCtl = new knjd192dController;
//var_dump($_REQUEST);
?>
