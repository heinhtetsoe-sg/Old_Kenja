<?php

require_once('for_php7.php');

require_once('knjl140cModel.inc');
require_once('knjl140cQuery.inc');

class knjl140cController extends Controller
{
    public $ModelClassName = "knjl140cModel";
    public $ProgramID      = "KNJL140C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl140cForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl140cCtl = new knjl140cController();
