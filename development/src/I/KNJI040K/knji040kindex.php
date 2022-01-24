<?php

require_once('for_php7.php');

require_once('knji040kModel.inc');
require_once('knji040kQuery.inc');

class knji040kController extends Controller {
    var $ModelClassName = "knji040kModel";
    var $ProgramID      = "KNJI040K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knji040kForm1");
                    break 2;
                case "search_view":	    //検索画面
                case "search_view2":	//検索画面（卒業年度）
                    $this->callView("knji040kSearch");
                    break 2;
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "knji040k":                                //メニュー画面もしくはSUBMITした場合
//                  $sessionInstance->knji040kModel();       コントロールマスタの呼び出し
                    $this->callView("knji040kForm1");
                    break 2;
                case "":
                    $this->callView("knji040kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji040kCtl = new knji040kController;
//var_dump($_REQUEST);
?>
