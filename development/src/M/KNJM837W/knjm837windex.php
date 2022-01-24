<?php

require_once('for_php7.php');

require_once('knjm837wModel.inc');
require_once('knjm837wQuery.inc');

class knjm837wController extends Controller
{
    public $ModelClassName = "knjm837wModel";
    public $ProgramID      = "KNJM837W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjm837wForm1");
                    }
                    break 2;
                case "":
                case "knjm837w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm837wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm837wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm837wCtl = new knjm837wController();
//var_dump($_REQUEST);
