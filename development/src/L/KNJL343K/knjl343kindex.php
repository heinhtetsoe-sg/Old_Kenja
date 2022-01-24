<?php
require_once('knjl343kModel.inc');
require_once('knjl343kQuery.inc');

class knjl343kController extends Controller {
    var $ModelClassName = "knjl343kModel";
    var $ProgramID      = "KNJL343K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343k":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl343kModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl343kForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl343kForm1");
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
$knjl343kCtl = new knjl343kController;
//var_dump($_REQUEST);
?>
