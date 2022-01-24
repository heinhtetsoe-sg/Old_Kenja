<?php

require_once('for_php7.php');

require_once('knjd106cModel.inc');
require_once('knjd106cQuery.inc');

class knjd106cController extends Controller {
    var $ModelClassName = "knjd106cModel";
    var $ProgramID      = "KNJD106C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd106c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd106cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd106cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd106cForm1");
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
$knjd106cCtl = new knjd106cController;
//var_dump($_REQUEST);
?>
