<?php

require_once('for_php7.php');

require_once('knjd105mModel.inc');
require_once('knjd105mQuery.inc');

class knjd105mController extends Controller {
    var $ModelClassName = "knjd105mModel";
    var $ProgramID      = "KNJD105M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105m":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105mForm1");
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
$knjd105mCtl = new knjd105mController;
//var_dump($_REQUEST);
?>
