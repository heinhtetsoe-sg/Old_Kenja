<?php

require_once('for_php7.php');

require_once('knjl455yModel.inc');
require_once('knjl455yQuery.inc');

class knjl455yController extends Controller
{
    public $ModelClassName = "knjl455yModel";
    public $ProgramID      = "KNJL455Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl455yForm1");
                    }
                    break 2;
                case "":
                case "knjl455y":
                    $sessionInstance->knjl455yModel();
                    $this->callView("knjl455yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl455yCtl = new knjl455yController();
