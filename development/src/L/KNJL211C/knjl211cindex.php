<?php

require_once('for_php7.php');

require_once('knjl211cModel.inc');
require_once('knjl211cQuery.inc');

class knjl211cController extends Controller {
    var $ModelClassName = "knjl211cModel";
    var $ProgramID      = "KNJL211C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl211c":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl211cModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl211cForm1");
                    exit;
                case "csv":         //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl211cForm1");
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
$knjl211cCtl = new knjl211cController;
?>
