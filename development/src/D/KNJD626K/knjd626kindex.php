<?php

require_once('for_php7.php');

require_once('knjd626kModel.inc');
require_once('knjd626kQuery.inc');

class knjd626kController extends Controller
{
    public $ModelClassName = "knjd626kModel";
    public $ProgramID      = "KNJD626K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd626kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626kForm1");
                    exit;
                case "knjd626kChangeGroupDiv":
                case "knjd626k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd626kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626kForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd626kForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd626kCtl = new knjd626kController();
//var_dump($_REQUEST);
