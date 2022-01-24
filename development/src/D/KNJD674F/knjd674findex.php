<?php

require_once('for_php7.php');

require_once('knjd674fModel.inc');
require_once('knjd674fQuery.inc');

class knjd674fController extends Controller {
    var $ModelClassName = "knjd674fModel";
    var $ProgramID      = "KNJD674F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd674f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd674fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd674fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd674fForm1");
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
$knjd674fCtl = new knjd674fController;
//var_dump($_REQUEST);
?>
