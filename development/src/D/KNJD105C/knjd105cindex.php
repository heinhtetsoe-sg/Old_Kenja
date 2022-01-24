<?php

require_once('for_php7.php');

require_once('knjd105cModel.inc');
require_once('knjd105cQuery.inc');

class knjd105cController extends Controller {
    var $ModelClassName = "knjd105cModel";
    var $ProgramID      = "KNJD105C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105c":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105cForm1");
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
$knjd105cCtl = new knjd105cController;
//var_dump($_REQUEST);
?>
