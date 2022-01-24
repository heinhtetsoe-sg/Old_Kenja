<?php

require_once('for_php7.php');

require_once('knjd173hModel.inc');
require_once('knjd173hQuery.inc');

class knjd173hController extends Controller {
    var $ModelClassName = "knjd173hModel";
    var $ProgramID      = "KNJD173H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd173h":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd173hModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd173hForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd173hForm1");
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
$knjd173hCtl = new knjd173hController;
//var_dump($_REQUEST);
?>
