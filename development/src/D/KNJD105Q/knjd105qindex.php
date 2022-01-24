<?php

require_once('for_php7.php');

require_once('knjd105qModel.inc');
require_once('knjd105qQuery.inc');

class knjd105qController extends Controller {
    var $ModelClassName = "knjd105qModel";
    var $ProgramID      = "KNJD105Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105q":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_seme":
                case "change_major":
                case "change_testcd":
                case "change_grade":
                    $sessionInstance->knjd105qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105qForm1");
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
$knjd105qCtl = new knjd105qController;
//var_dump($_REQUEST);
?>
