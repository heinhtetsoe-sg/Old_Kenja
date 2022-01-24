<?php

require_once('for_php7.php');

require_once('knjd105hModel.inc');
require_once('knjd105hQuery.inc');

class knjd105hController extends Controller {
    var $ModelClassName = "knjd105hModel";
    var $ProgramID      = "KNJD105H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105h":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105hModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105hForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105hForm1");
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
$knjd105hCtl = new knjd105hController;
//var_dump($_REQUEST);
?>
