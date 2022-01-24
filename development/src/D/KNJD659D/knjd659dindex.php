<?php

require_once('for_php7.php');

require_once('knjd659dModel.inc');
require_once('knjd659dQuery.inc');

class knjd659dController extends Controller {
    var $ModelClassName = "knjd659dModel";
    var $ProgramID      = "KNJD659D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd659d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd659dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd659dForm1");
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
$knjd659dCtl = new knjd659dController;
//var_dump($_REQUEST);
?>
