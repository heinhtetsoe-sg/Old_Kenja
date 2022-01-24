<?php

require_once('for_php7.php');

require_once('knjd105eModel.inc');
require_once('knjd105eQuery.inc');

class knjd105eController extends Controller {
    var $ModelClassName = "knjd105eModel";
    var $ProgramID      = "KNJD105E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105e":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105eModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105eForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105eForm1");
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
$knjd105eCtl = new knjd105eController;
//var_dump($_REQUEST);
?>
