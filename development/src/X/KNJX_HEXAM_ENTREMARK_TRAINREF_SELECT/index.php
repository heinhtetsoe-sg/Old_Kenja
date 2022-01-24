<?php
require_once('knjx_hexam_entremark_trainref_selectModel.inc');
require_once('knjx_hexam_entremark_trainref_selectQuery.inc');

class knjx_hexam_entremark_trainref_selectController extends Controller
{
    public $ModelClassName = "knjx_hexam_entremark_trainref_selectModel";
    public $ProgramID      = "knjx_hexam_entremark_trainref_select";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjx_hexam_entremark_trainref_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_hexam_entremark_trainref_selectCtl = new knjx_hexam_entremark_trainref_selectController();
